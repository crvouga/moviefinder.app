(ns core.env
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.string :as str]))

(defn load-env! []
  (let [env-path (.resolve path ".env")
        exists? (.existsSync fs env-path)]
    (when exists?
      (let [content (.readFileSync fs env-path "utf8")]
        (doseq [line (clojure.string/split content #"\n")]
          (let [[key value] (clojure.string/split line #"=" 2)]
            (when (and key value)
              (aset js/process.env (clojure.string/trim key) (clojure.string/trim value)))))))))

(load-env!)

(defn get! [key]
  (aget js/process.env key))

(defn get-else-throw! [key]
  (let [value (get! key)
        valid? (and (string? value) (str/trim value))]
    (when-not valid?
      (throw (js/Error. (str key " environment variable is not set"))))
    value))
