# FuriousIron-HFB

HFB Implementation - Proof of Concept

This is a proof of concept code for a hash-function free Bloom-filter, which I use for my 
personally developed source code search engine. It took me some iterations to simplify the
concept of a Bloom-filter that much, that there is now basically no algorithm left. Calling
this Hash-Free-Bloom-Filter an algorithm would overstate the two lines of implementing code
but simplifying Bloom-filters down to basically two lines of code is nonetheless art.

If you like this approach or cite it, please link back to this repository and cite this 
project URL using:

    'Hash-Free-Bloom-Filters'. Maxim Gansert. 2022. (https://github.com/mindscan-de/FuriousIron-HFB).

Maybe I should write a paper on it... 

## Hash-Free-Bloom (HFB)

HFB stands for Hash-Free-Bloom. I'm not sure whether someone already did something similar
or not, but I'm not bothered enough to figure that out, nor I'm aware of a publication
documenting this approach. Essentially this is a way which I came up with, to implement a 
test for "doesn't contain".

Bloom-filters answer the question, whether a value is definitely not included in a set. 
Bloom-filters can't answer with a sure 'yes'. Each 'yes' is either a sure 'yes' or a 
false positive but a 'no' is always a 'no'. Anyhow when implementing a search engine, 
the scenario basically is, to remove all the documents which don't contain one or more 
search terms, which we are specifically searching for.

A Hash-Free-Bloom-Filter comes with a kind of clever hash function, which allows the hash
to be computed that efficiently that it becomes effectively indistinguishable from a memory
lookup. The hash function presented in this article creates a virtually hash-function-free 
Bloom-Filter. 

The whole computational effort for the hash-function presented in this article, has the
equivalence of only two CPU instructions followed by the indexed memory lookup for every
applied Bloom-filter. 

Hash-Free-Bloom-Filters work best in special circumstances only, which I want to outline
next.

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

### Document IDs

Document IDs are treated as an identifier of the real document. This identifier is
calculated only once for every document, once it enters the search index.

In a proof-of-concept we are looking whether a certain document ID is contained in a set of 
document IDs using a HFB-FilterBank. The first important point for the document ID is, that
it is a result of a collision resistant hash function (CRHF) or can be derived by sampling 
from a random process. The second important point is that this document ID is preferably 128
bit in size or longer. I see no reason, why this algorithm shouldn't also work with shorter
document IDs as well e.g (64 bit).

A document ID can be obtained by hashing the content, or by hashing the origin / location of
this document, but is not limited to that. This hash value becomes the document ID and can be
used to address / identify a particular document or the content of a particular document. 

### Bloom-Filters

A Bloom-filter hashes a given value and uses this hash value to look it up in some kind
of array whether this memory location contains either a zero or a non-zero value.
 
A zero value indicates that such a hash value was never hashed during the insertion stage of
the Bloom-filter. A non-zero value indicates, that, either the hash value was hashed during
the insertion phase, or a different value calculated a collision resulting in the same hash.
 
Therefore a non-zero value means that the searched value was maybe part of the data set
inserted into the Bloom-filter.
 
When we repeat this question with different hash functions for the same value and then 
repeat the lookup in a different array, the risk of a false positive, reduces with each 
different applied hash function and their calculated hash values. Many thoughts have 
gone into reducing the amount of hash functions, to compute multiple hash values.

## Hash-Free-Bloom-Filters

Bloom-filters need a hash function to work. The reason for the hash function is to distribute
input values across some output set in a random fashion. Usually a uneven input distribution
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

The hash values of CRHF are already distributed evenly, and every single output bit has an 
even distribution. Each and every output bit is uncorrelated to every other bit of the hash 
function.

So instead of using another function like Murmur, Adler32, CRC or any other home-grown 
algorithm, which again garbles the full 128 bit using a computationally expensive function, 
we can conclude that we can simply extract/sample hash values of any size (smaller than the
output size of the hash function) from the already computed 128-bit hash result.

That means, that we simply sample the hash value itself, this will already provide enough 
evenly distributed hash values for a Bloom-filter.

## Sampling the Document ID

Sampling the document IDs is actually quite simple. This can be done with a 'right-shift'
operation (SHR) and an 'and' operation (AND), other ways are bit extract operations (BEXTR).
At least SHR and AND are both basic CPU operations, which are present since the very first 
microprocessor generations. They usually take only one CPU clock cycle each.

Therefore a new parameterizable hash function can be defined, with two parameters ``start`` 
and ``len``: 

    H_start_len_(document_id) = (document_id >> start) & ((1 << len) - 1)
    
where ``document_id`` is the document id we want to test or insert into a Bloom-filter array,
and ``start`` is the start position from where we want to extract ``len`` bits in a row.

    H_20_10(X) = (X >> 20) & 0x03ff
    
This is how a hash extractor can be parameterized and how to extract hash values from a
larger hash value, using two degrees of freedom.

If multiple independent hash values are needed, each of 10 bit length: We can use H_0_10, 
H_10_10 and H_20_10 or any other ```start```value, where ``start + len <= ||document_id||``
is satisfied. If ten hashes with length 12 are required then use H_0_12, H_12_12, H_24_12 ... H_108_12.

These hash values are uncorrelated to each other, as long as their extractions don't overlap. As 
long as 'start' of one hash function is not the range of [start of second .. start of second+len]
and the other way around. They are uncorrelated because a CRHF or a random process was used to 
create these document IDs in the first place. If multiple hash functions are required for these
document IDs, then we just select a non-overlapping  ``start`` parameter for each of them.

This extracted hash value usually represents the index in memory, where to look and to decide
whether this hash value was known before or not, during the insert phase into a Bloom-filter.

The full access looks something like this to access the HFB filter data using (``H_20_10``),
for every document ID denoted as ``X``:

    hfb_filter_data[(X >> 20) & 0x03ff]

This is indistinguishable from a simple bounded memory access. A combination of hfb_filter_data
and a parameterized hash function is called a HFB filter bank. Each HFB filter bank has its own
hfb_filter_data and its own parameterized hash function (start, len) for its particular set of 
document IDs.

A HFB filter is a collection of one or multiple HFB filter bank(s).

The catch is, that we can directly operate on the document ID for this HFB filter, without
spending additional compute for another hash function and those bound limits (AND operation) 
would be implemented and required anyways, to avoid out of bound memory accesses. This means 
that a single SHR operation on a document_id replaces all the compute effort of a hash function 
implementation nevertheless how fast and efficient it is. A single SHR operation as a 
replacement for a hash-function will computationally wise not be beaten.

## Controlling ``len`` to Control the Reject Rate

Each set of documents stored in a HFB filter bank has its own parameterizable hash function 
having two parameters, ``start`` and ``len``. We could see that we can control the number of
uncorrelated hash functions using ``start`` and ``len``.

Lets assume the number of inserted document IDs into the HFB-Filter is 12000. A ``len`` of
12 bits (representing 4096 memory positions) will lead to only very few memory positions 
containing a zero value, considering the random nature of the document ID generation. 

If a ``len`` of 14 bits (representing 16384) a maximum of 12.000 memory positions are
non-zero after the insertion phase. Usually less than 12000 memory positions are non-zero, 
because of collisions (this creates more memory positions containing zeros). About 25%
of memory positions are zero.

If a ``len`` of 16 bit (representing 65536) is selected, still a maximum of 12.000 memory 
positions are non-zero there can't be more positions filled after the insertion phase. That 
means that there are now >80% memory positions containing a zero value.

Therefore a random test would have a rejection rate of 25% (for len==14) and over 80% 
(for len==16) for a randomly selected document ID. The number of zero positions determines
the reject rate and therefore inversely the false positive rate for one particular filter
bank. 

The more zeros in the filter bank data compared to the available memory positions, the higher 
the rejection rate. The lesser non-zero values compared to the available memory positions the
lesser the false positive rate. 

Another way to reduce the false positive rate is to use multiple filter banks for the test
in a serial manner. You test with the first filter bank it either responds with 'maybe' or 
'no'. But then you ask the second filter bank, which is using its own hash function to re
test the same document ID, and if it was a false positive the chances are good that it will
be identified as a false positive with still a 'maybe' or a 'no'. In case of a 'no' the 
first test was a false positive. And so on. You either can continue until you checked all
HFB filter banks or you stop, when your confidence is high enough.

Lets assume a 80% rejection rate and therefore a 20% false positive rate

* 1st iteration using first filter: 20% likelihood to be a false positive
* 2nd iteration using second filter: 4% likelihood still to be a false positive
* 3rd iteration using third filter: 0.8% likelihood still to be a false positive
* 4th iteration using fourth filter: 0.16% likelihood still to be a false positive
* 5th iteration using fifth filter: 0.032% likelihood still to be a false positive

You usually can reach any desired confidence, by the number of tested filter banks 
and via the ``len`` parameter, which controls for the false positive rate inversely.
Each extra bit in ``len`` will double the amount of memory required for the test.

You can optimize the usage of the number of applied filter banks and memory size and
storage to your needs and requirements, based on the number of documents inserted into 
a certain HFB filter. (But this is a whole complex topic in itself.)

## Filter Effectiveness Consideration leads to better Performance

----
TODO: rework this.


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