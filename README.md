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

In this proof-of-concept we are looking whether a certain document ID is contained in a set of 
documentIDs using a HFB-FilterBank. The important point is, that the documentID is a result
of a collision resistant hash function (CRHF). The document ID of this hash function can either 
be obtained by hashing the content or by hashing the origin of this document. The hash value
can now be used to address a particular document or the content of a particular document.

