# FuriousIron-HFB

HFB Implementation - Proof of Concept

## Hash-Free-Bloom  (HFB)

HFB stands for Hash-Free-Bloom I'm not sure whether someone already did something similar or
not, but I'm not bothered enough to figure that out. Essentially this is a way which I came 
up with, to implement a test for "doesn't contain".

I looked for an efficient way to effectively test whether a document contains a certain search 
term or not.

Bloom filters answer the question whether a value is not included. But can't answer a sure
yes. Each yes is either a sure yes or a false positive. But a no is always a no. Anyhow when 
implementing a search engine, the scenario basically is to drop potential documents which do 
not contain one or more search terms, which we are specifically looking for.

This Hash-Free-Bloom Filter comes with a kind of clever hash function, which allows the hash
to be computed that effectively that it is basically indistinguishable from a memory lookup. 
Making this a practically hash function free Bloom-Filter. The only remaining computational
effort is the memory lookup to decide, whether a candidate can be dropped or not.

## Document IDs

In a proof-of-concept we are looking whether a certain document ID is contained in a set of 
documentIDs using a HFB-FilterBank. The important point is, that the document ID is a result
of a collision resistant hash function (CRHF). The document ID of this hash function can be
obtained by hashing the content or by hashing the origin of this document for example. The
hash value can now be used to address a particular document or the content of a particular
document.

## Bloom-Filters

A bloom filter hashes a given value and uses its hash value to look up in some kind of 
array whether this memory location contains either a zero or a non-zero value. A zero
value indicates that such a value was not hashed. A non-zero value indicates that either
the searched value was hashed or a different value created a collision. Therefore a
non-zero value means that the searched value was maybe part of the document. If we repeat
this question with different hash functions for the same value and then do these lookups,
the risk of a false positive, reduces with each different calculated hash value.

## Hash Free Bloom Filters

Bloom filters need a hash function to work properly. But does it make any sense to make new
hash calculations for the result of a hash function. In case of MD5, as a CRHF we get 128-bit
output from the hash function. So instead of using a new hash function which again garbles
the full 128 bit in a computationally expensive function, we can decide that we can extract
hash values of any size from the already computed 128-bit hash result. We can do that by
a right-shift-operation and an and-operation. Therefore we can parameterize the new hash
function with basically two parameters, the number of bits to shift to the right and the 
number of bits to keep with an and operation (e.g. 10 bits to keep -- an and operation with 0x3ff)

This gives us a computationally very efficient hash function, which is indistinguishable from
a normal memory operation, where we ensure that the memory we access can exceed a defined
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
bloom filter, without spending additional compute for another hash function.

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

Golomb Coding produces low overhead even if hash size increase, then the distance between two
consecutive non zero values just increases, leading to a different value of "m". So the storage
on disk doesn't change significantly because of the more sparse array of non-zero values. Because
a fixed number of document IDs is inserted into the filter bank, the number of bits in an array 
has an upper bound by the number of inserted values, making the number of zero runs just more 
likely, which can be accommodated by the Golomb coding parameters. Leading to a small increase
compressed filter data size.

That means that more sparse arrays may need more temporary memory requirements when testing, but
they don't need much more permanent storage on disk. More sparse arrays lead to higher per filter
bank dropout. But are used only for a short time in memory. Once the filter is applied to a list
of document IDs or hashed keys, its filter banks job is done.

## Privacy preserving aspect

If only 3 out of all filter banks are saved to disk, the document ids from the set can not be 
extracted from the filter data effectively. Because the filter saved the data with the highest
number of collisions first. And if only 45 bits out of 128 are saved to disk, the missing bits
can not be reconstructed.

## TLDR

This is basically the most computationally effective hash function for a bloom filter. 

document_id - result of a CRHF (collision resistant hash function, e.g. 128 bit (MD5) or longer)
slice_position - bit position where the hash is extracted from the document id
slice_mask - the lowest n bits depending on (output size, number of document IDs, sparsity, dropout-rate) are set, all other are zero.

Hash calculation 

    extracted_hash = (document_id >> slice_position) & slice_mask

Perform Test: 

    hfbfilterdata[extracted_hash]!=0

This is basically a bloom filter implementation done in two lines of code. Which is basically 
indistinguishable from a bounded memory access. The idea is to basically skip the hashing of
an already CRHF generated hash value and replacing it by hash value extraction. In case you 
want to test a list of hashed document origins against a set of a list of hashed document 
origins.