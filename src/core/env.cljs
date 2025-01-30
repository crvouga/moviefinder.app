(ns core.env
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.string :as str]))

(defn- parse-env-line [line] (str/split line #"=" 2))

(defn- valid-env-pair? [[k v]] (and k v))

(defn- set-env! [[k v]]
  (aset js/process.env (str/trim k) (str/trim v)))

(defn- read-env-file! [path]
  (->> (.readFileSync fs path "utf8")
       (str/split #"\n")
       (map parse-env-line)
       (filter valid-env-pair?)
       (run! set-env!)))

(defn- load-env! []
  (let [env-path (.resolve path ".env")]
    (if-not (.existsSync fs env-path)
      (js/console.warn "No .env file found")
      (read-env-file! env-path))))

(defn get! [key]
  (load-env!)
  (aget js/process.env key))

(defn get-else-throw! [key]
  (let [value (get! key)
        valid? (and (string? value) (str/trim value))]
    (when-not valid?
      (throw (js/Error. (str key " environment variable is not set"))))
    value))
