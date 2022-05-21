# FuriousIron-HFB

HFB Implementation - Proof of Concept

This is a proof of concept code for a hash-function free Bloom-filter, which I use for my 
personally developed source code search engine. It took me some iterations to simplify the
concept of a Bloom-filter that much, that there is now basically no algorithm left. Calling
this Hash-Free-Bloom-Filter an algorithm would overstate the two lines of implementing code
but simplifying Bloom-filters down to basically two lines of code is nonetheless art.

If you like this approach or cite it please link back to this repository and cite this 
project URL.

Maybe I should write a paper on it...

Please enjoy:

## Hash-Free-Bloom  (HFB)

HFB stands for Hash-Free-Bloom I'm not sure whether someone already did something similar or
not, but I'm not bothered enough to figure that out. Essentially this is a way which I came 
up with, to implement a test for "doesn't contain".

Bloom-filters answer the question whether a value is not included. But can't answer a sure
yes. Each yes is either a sure yes or a false positive. But a no is always a no. Anyhow when 
implementing a search engine, the scenario basically is to drop potential documents which do 
not contain one or more search terms, which we are specifically looking for.

This Hash-Free-Bloom-Filter comes with a kind of clever hash function, which allows the hash
to be computed that effectively that it is basically indistinguishable from a memory lookup. 
Making this a practically hash function free Bloom-Filter. The only remaining computational
effort is a memory lookup to decide, whether a candidate can be dropped or not.

Hash-Free-Bloom-Filters work only in a special circumstance, which I want to outline next. 

## Preconditions

I looked for an efficient way to effectively test whether a document may contain a certain 
search term or not by using an inverse index. For each trigram a complete list of document
IDs is maintained, where this particular trigram is included in the document at least once.

In case a word or phrase is searched for, every possible trigram is generated from the 
search query term. The intermediate candidate documents are only those, where the same 
document ID is listed for every of these trigrams. 

The whole search operation works mostly exclusively on document IDs for the very first 
candidate stage. Also the metadata search is working on the same document IDs as well.

The HFB filter doesn't look for the content of the document, but rather weeds out, documents
where the particular search term is technically not even possible, so that no time is wasted
on them.

## Document IDs

Document IDs are treated as an identifier to the real document. This identifier is
calculated only once for every document, once it enters the search index.

In a proof-of-concept we are looking whether a certain document ID is contained in a set of 
document IDs using a HFB-FilterBank. The important point for the document ID is, that it is 
a result of a collision resistant hash function (CRHF). 

The document ID of this hash function can be obtained by hashing the content or by hashing 
the origin of this document for example. This hash value can be used to address / identify 
a particular document or the content of a particular document.

## Bloom-Filters

A Bloom-filter hashes a given value and uses this hash value to look up in some kind
of array whether this memory location contains either a zero or a non-zero value.
 
A zero value indicates that such a hash value was never hashed during the insertion stage of
the Bloom-filter. A non-zero value indicates that, either the hash value was hashed during
the insertion phase, or a different value created a collision resulting in the same hash.
 
Therefore a non-zero value means that the searched value was maybe part of the data set
inserted into the Bloom-filter.
 
When we repeat this question with different hash functions for the same value and then 
repeat the lookup in a different array, the risk of a false positive, reduces with each 
different calculated hash value.

## Hash-Free-Bloom-Filters

Bloom-filters need a hash function to work. The reason for the hash function is to distribute
input values across some output set in a random fashion. Usually a non-even input distribution
is mapped to a more even output distribution.

But since we use the document ID and want to check, whether it is included a set of
known document IDs, any hash function would basically try to evenly distribute a CRHF 
derived hash value. CRHF main objective is to have a hash function which is collision 
resistant. 

From a point of view, since we can't predict the output of a hash value, it is basically
random. Putting random values into another hash function is just a waste of CPU-cycles.

In case of MD5, as a CRHF we retrieve an unpredictable and well distributed 128-bit output 
from the MD5-hash function. Any other cryptographic hash function, MD4, SHA1, SHA256, or
any other Message Authentication Code already did the job, to create an evenly distributed
but non predictable output. Although it can be calculated rather than predicted. We are
actually not interested in the properties that cryptographic hash functions are considered
as one-way-hash-functions, but that doesn't hurt either. 

So instead of using another function like Murmur, Adler32, CRC or any other home-grown 
algorithm, which again garbles the full 128 bit using a computationally expensive function, 
we can conclude that we can simply extract/sample hash values of any size (smaller than the
output size of the hash function) from the already computed 128-bit hash result.

That means we simply sample the hash value itself, this would already provide enough 
evenly distributed hash values for the Bloom-filter.

## Sampling the Cryptographic Hash Value

----
TODO: rework this.

We can do that by
a right-shift-operation and an and-operation. Therefore we can parameterize the new hash
function with basically two parameters, the number of bits to shift to the right and the 
number of bits to keep with an and operation (e.g. 10 bits to keep -- an and operation with 0x3ff)

Instead of a hash value calculation we simply extract hash values from a larger hash value.

This gives us a computationally very efficient hash function, which is indistinguishable from
a normal memory operation, where we ensure that the memory we access can't exceed a defined
size. We also can extract multiple independent hash values, one size 10 with shift zero,
or one with a size of 22 bit a bit shift by 44. The hash vales do not correlate, since their
input hash value is computed and created once using a CRHF. Therefore "computing" different
hash values doesn't require to invent different hash functions, we just go for a different
combination of bits to right shift and hash function output size.

So lets say we need 10 different hash values of 12 bit output length, we can shift by
0,12,24,36,48,60,72,84,96,108 and and with 0xfff to get 10 different easy to calculate
hash values. Or we shift by 1,13,25,...,109 and so on. 

With that particular calculated output hash value we can directly access any array, and do 
the readout whether this document id is still a candidate worth inspecting or is eliminated 
by the Bloom-Filter. The catch is, that we can directly operate on the document id for this 
Bloom-filter, without spending additional compute for another hash function.

## Different Hash Sizes to Control reject rate

Each Set of documents stored in a HFB-Filter can provide its own "parameterizable" hash 
function. If the number of inserted document ids into the HFB-Filter is 12000. Then you 
can choose either to use 10 bits, which will yield in a low rejection rate, whereas an 
output size of 16 bit, will yield a nearly 80% reject rate. For the application of one 
filter so the memory consumption and the computational effort can still be adjusted. For
my cases i usually want a 80% rejection rate for a filter configuration, but only execute
three of these filters.

## Filter Effectiveness Consideration leads to better Performance

After inserting the documents each particular filter banks effectiveness can be calculated,
by the number of non-zero positions in the filter bank data. By saving these with the lowest
weight first we can choose to use the best filters first (saving those with the highest dropout
rate first), that effectively is leading to a randomization in using the filters and a better
performance to dropout-rate for the candidate document IDs.

Since the most effective filters for a particular document set are used first, the number of
false positives in the application of the first filter is lowest, then we use the hash function
which lead to a filter with the second lowest number of non-zero values, produces again the 
second lowest number of false positives and so on. the first applied filter might be the one 
with a bit shift of 84 and the second one with a bit shift by 24 and the third one with a bit
shift by 108. This kind of natural randomization is useful, to not test the same bits over and
over again. That will avoid to spend compute on document ids with filters with low quality 
(those with a low rejection) rate.

And because you know the false positive rate, you don't need to execute all filter banks, but
the most effective ones first until you reach a false positive rate of maybe 0.1%. Then you 
can stop saving that filter data to disk (saving then I/O and storage) for the HFB-Filters.

You can optimize storage size vs. filter efficiency. by using e.g. one bit less for the
output hash function, but spending compute one extra filter.

## Golomb coding for HFB-FilterData

Golomb Coding produces low overhead even if outout hash size increases, then just the distance between 
two consecutive non zero values just increases, leading to a different value of "m". So the storage
on disk doesn't change significantly because of the more sparse array of non-zero values. Because
a fixed number of document IDs is inserted into the filter bank, the number of bits in an array 
has an upper bound by the number of inserted values, making the number of zero runs just more 
likely, which can be accommodated by the Golomb coding parameters. Leading to a small increase in
compressed filter data size.

That means that more sparse arrays will need more temporary memory requirements when testing, but
they don't need much more permanent storage on disk. More sparse arrays lead to higher per filter
bank drop-out. But only stay for a short time in memory. Once the filter is applied to a list of 
document IDs or hashed keys, its filter banks job is done.

## Privacy preserving aspect

If only 3 out of all filter banks are saved to disk, the document ids from the set can not be 
extracted from the filter data effectively. Because the filter saved the data with the highest
number of collisions first. And if only 45 bits out of 128 are saved to disk, the missing bits
can not be reconstructed.

## Combine HFB-Filters with other Bloom-Filter solutions

All the other modifications, like counting Bloom-Filters are still possible - I just replaced
the hash function with something very very simple. 

## TLDR

This is basically the most computationally effective / fastest hash function for a Bloom-filter. 

* ``document_id`` - result of a CRHF (collision resistant hash function, e.g. 128 bit (MD5) or longer)
* ``slice_position`` - bit position where the hash is extracted from the ``document_id``
* ``slice_mask`` - the lowest n bits depending on output size, number of document IDs, sparsity or desired drop-out-rate are set, all other are set to zero

Hash calculation / Hash extraction

    extracted_hash_value = ( document_id >> slice_position ) & slice_mask
    
Insert DocumentId

    hfbfilterdata [ extracted_hash_value ] = 1

Perform Test: 

    hfbfilterdata [ extracted_hash_value ] != 0

This is basically a Bloom-filter implementation done in two lines of code. Which is basically 
indistinguishable from a bounded memory access. The idea is to basically skip the hashing of
an already CRHF generated hash value and replacing it by hash value extraction. In case you 
want to test a list of hashed document origin IDs against a set of hashed document 
origin IDs.