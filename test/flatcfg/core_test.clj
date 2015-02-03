(ns flatcfg.core-test
  (:refer-clojure :rename {get clj-get
                           get-in clj-get-in})
  (:require [clojure.test :refer :all]
            [flatcfg :refer :all]))

(deftest core-tests
  (is (=
       (key->vector (keywordize "db.name"))
       (key->vector (vector->key (key->vector (keywordize "db.name"))))))

  (is (= (keywordize "db.name")
         :db-name))

  (is (= (keywordize "db.name")
         (keywordize (keywordize "db.name"))))

  (is (= "default-value"
         (get! :this-key-doesnt-exists "default-value"))))


(deftest config-tests
  (reset! file-config {:deep {:test "deep-value"}
                       :deep-value "hi there"
                       :cmd-arg "bo bo bo"})

  (def my-arg (get :cmd-arg))
  (def my-arg2 (get-in :cmd-arg))

  (consume-args ["cmd-arg" "hello from admin"])

  (is (= (get-in! :deep :test)
         (get! :deep-test)
         "deep-value"))

  (is (= (get-in! :deep :value)
         (get! :deep-value)
         "hi there"))

  (is (= (get! :cmd-arg)
         (get-in! :cmd :arg)
         @my-arg
         @my-arg2
         "hello from admin")))
