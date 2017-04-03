# Paw: A Tokenizer plugin for GreyCat


![GreyCat-version](https://img.shields.io/badge/GreyCat--version-6--SNAPSHOT-green.svg)

![version](https://img.shields.io/badge/version-0.1--SNAPSHOT-blue.svg)

This library bring additional functionality to the [GreyCat](https://github.com/datathings/greycat) project. 
This library is compatible with the latest Greycat api. 

### Token?

This project aims at providing a way to tokenize all of string that one might want to store in the graph.
In addition to saving space, it also enable further analytics. Every tokenized content being accessible through a vocabulary node globally indexed. 
The library supports time and world, every new word being created at the Beginning of Time and first world.

### Tokenizer

Currently the library embed several Tokenizer

* Identity Tokenizer (return the exact same string)
* Simple Tokenizer, space based
* UTF Tokenizer
* English Tokenizer
* Twitter Tokenizer
* C Tokenizer
* CPP Tokenizer
* Java Tokenizer

Some preprocessors are available to all Tokenizer:

* Lower Case
* Upper Case

More can be implemented on request.

An option to keep all delimiters is already offered for most Tokenizer(at the exception of the C and C++ ones).

### Before Use notice

The plugin can be used on any already existing graph, however make sure that all your global index were created at the beginning of time as it is the default behaviour of the library to create global index at the beginning of time. Already existing global index created after the beginning of time would create a modification of the past that would create unpredictable side effects.

### How does it work?
![schema](doc/schema.png)


### Set of Provided Actions

* Initialize Voacabulary
* Retrieve Vocabulary Node
* Get or create Token From String
* Tokenize String Using Tokenizer
* Create or Update Tokenize Relation to Node


### How to use this library?

In progress
