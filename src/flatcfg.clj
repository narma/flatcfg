(ns flatcfg
  (:refer-clojure :rename {get clj-get
                           get-in clj-get-in})
  (:require [clojure.edn]
            [environ.core :refer [env]]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(defn keywordize
  [s]
  (if (keyword? s)
    s
    (-> (str/lower-case s)
        (str/replace "_" "-")
        (str/replace "." "-")
        keyword)))

(defn key->vector [k]
  (mapv keyword (-> k
                    name
                    (str/split #"-"))))

(defn vector->key [k]
  (->> k
       (map name)
       (str/join "-")
       keyword))

(def args-config (atom {}))
(def file-config (atom {}))


(defn get!
  ""
  ([key]
   (get! key nil))
  ([key default]
   (let [k (keywordize key)]
     (or (clj-get @args-config k)
         (clj-get env k)
         (clj-get @file-config k)
         (clj-get-in @file-config (key->vector k))
         default))))

(defn get-in!
  [& args]
  (or (get! (vector->key args))
      (clj-get-in @file-config args)))


(defn get [& args]
  (delay (apply get! args)))

(defn get-in [& args]
  (delay (apply get-in! args)))

(defn load-config-file [filename]
  (with-open [in (java.io.PushbackReader. (io/reader filename))]
    (reset! file-config (clojure.edn/read in))))


(defonce ^:private filecfg-from-env-or-default
  (let [cfg-filename
        (or (env :config)
            (.getPath (io/file
                       (System/getProperty "user.dir")
                       "config.edn")))
        cfg-file (io/file cfg-filename)]
  (when (.exists cfg-file)
    (load-config-file cfg-filename))))


(defn args->hashmap
  [args]
  (let [even-args (if (even? (count args))
                args
                (butlast args))]
  (apply hash-map even-args)))


(defn consume-args
  [args]
  (let [args-dict (args->hashmap args)
        cfg-file (io/file (clj-get args-dict "config"))
        config-args (->> args-dict
                         (map (fn [[k v]] [(keywordize k) v]))
                         (into {}))]

    (when (and cfg-file (.exists cfg-file))
      (load-config-file (.getPath cfg-file)))

    (reset! args-config config-args)))


(consume-args ["port" "8000"])
