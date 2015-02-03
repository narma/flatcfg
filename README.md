# Flatcfg

Simple, easy-to-use clojure library for configure projects.
Because smart things must be handled by admins or devops.


Configuration supports three sources, resolved in the following order:

1. Command-line args
2. From environment by [environ](https://github.com/weavejester/environ) library.
3. Config-file


## Installation

```
[flatcfg "0.1.0"]
```


## Usage

Main point is equal config key from any source.
For example

* `AUTH_TWITTER_KEY=val` from ENV is equal to
* `-Dauth.twitter.key=val` from Java system properties is equal to
* `auth-twitter-key val` from command line is equal to
* `{:auth {:twitter {:key "val"}}}` from config file is equal to
* `{:auth-twitter-key "val"}` from another config file.

And it can be accessed by simple api:
```clojure
(get! :auth-twitter-key)
;; or
(get-in! :auth :twitter :key)
```

* As environ does all keys from command-line, args are lowercased, characters "_" and "." replaced by "-" and converted to keyword.

* By default *flatcfg* will try to read config from `config.edn` in same directory where `jar file` or `lein project` are located.

* Config file location can be set by `config` parameter in ENV or in cmd-args.
If it found, *flatcfg* will read it automatically.


### API
* `(get! key [default])` → config value
* `(get-in! [& args])` → config value
* `(get key [default])` → same as `(delay (get! key [default])`
* `(get-in [& args])` → same [Delay object](http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/delay)
* `(consume-args [args])` ← load args as command-line args
* `(load-config-file [filename])` ← load config from file

Delay object is required if we want to define config value at top-level side
but config will not be ready yet because it is filled later by `consume-args` for example.

### Nested structure in config file

A key in nested structure at EDN config file can be accessed by the key with "-" as separate character. For example for this config file:

```clojure
{:auth {:twitter {:key "super-secret-key-here"}}}
```


```clojure
(get! :auth-twitter-key)
;; the same as
(get-in! :auth :twitter :key)

```

BTW, `AUTH_TWITTER_KEY=my-secret-here java -jar app.jar` will work in the same way as config file, see below.


### Config examples
All config examples from below are equal.

* From command-line arguments: `java -jar app-standalone.jar db-name testing`
* From environment by `environ`:
 * `DB_NAME=testing java -jar app-standalone.jar`
 * `java -Ddb.name=testing -jar app-standalone.jar`
 * Advanced usage at [environ site](https://github.com/weavejester/environ), for example consume settings from the Leiningen project map.
* From EDN config file
```
{:db {:name "testing"}}
```
* Or in that way
```
{:db-name "testing"}}
```


### Code example

```clojure
(ns main
 (:require  [clojure.java.io :as io]
            [flatcfg :as cfg])
 (:gen-class))

(defn -main [& args]
  (cfg/consume-args args)
  ;; if we want not default behaviour
  (cfg/load-config-file (io/resource "config.edn"))
  (my-service/start))
```

```clojure
(ns my-service
  (:require [flatcfg :as cfg]))

(def my-secret-key (cfg/get-in :auth :secret-key))

(defn auth! []
  (use-my-secret @my-secret-key))
```



## License

Copyright © 2015 Sergey Rublev

Distributed under the MIT License.
