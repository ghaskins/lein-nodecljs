# lein-nodecljs

A [Leiningen](https://leiningen.org/) plugin that implements tasks useful for working with [ClojureScript](https://clojurescript.org/) on the NodeJS platform.

## What about 'lein-npm' and 'lein-cljsbuild'?

They are awesome, and historically this is what I have used too.  However, I also found that I was constantly
providing boilerplate in my projects to support outputting clojurescript to the nodejs platform.  For example:

* Configuring cljsbuild the same way to :target :nodejs
* Writing a Makefile to couple "lein npm deps" to "lein cljsbuild once"
* Manually solving the installation/launch of the program with a hacky script.

This plugin starts afresh with some fairly strong opinions on convenience utilities for working with
clojurescript specifically on the nodejs platform.  For example, this plugin completely overrides the the 'compile', 'run', and
'install' tasks.  It also provides a new task 'pack' which emulates 'npm pack', suitable for manual
installation or publishing to npmjs.org.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/lein-nodecljs.svg)](https://clojars.org/lein-nodecljs)

Put `lein-nodecljs` into the `:plugins` vector of your project.clj.

* `lein compile` - Override that compiles your program to :target/nodecljs.
* `lein run` - Override that compiles and then executes your program on the nodejs platform in place.
* `lein install` - Override that compiles and then installs your program with 'npm install -g'.
* `lein pack` - New task that packages your code with 'npm pack' suitable for manual installation or deployment to npmjs.org. 

## License

Copyright © 2017 Greg Haskins <gregory.haskins@gmail.com>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
