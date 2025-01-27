(ns core.tmdb-api.shared
  (:require [clojure.string :as str]
            [cljs.spec.alpha :as s]))

(s/def :tmdb/id number?)
(s/def :tmdb/title string?)
(s/def :tmdb/release-date string?)
(s/def :tmdb/overview string?)
(s/def :tmdb/poster-path string?)
(s/def :tmdb/vote-average number?)
(s/def :tmdb/vote-count number?)
(s/def :tmdb/popularity number?)
(s/def :tmdb/movie
  (s/keys :opt [:tmdb/id
                :tmdb/title
                :tmdb/release-date
                :tmdb/overview
                :tmdb/poster-path
                :tmdb/vote-average
                :tmdb/vote-count
                :tmdb/popularity]))
(s/def :tmdb/page number?)
(s/def :tmdb/total-pages number?)
(s/def :tmdb/total-results number?)
(s/def :tmdb/results
  (s/coll-of :tmdb/movie))

(s/def :tmdb/response
  (s/keys :req [:tmdb/results
                :tmdb/page
                :tmdb/total-pages
                :tmdb/total-results]))

(def ^:private base-url "https://api.themoviedb.org/3")

(defn namespace-key->external-key [k]
  (when (keyword? k)
    (-> k
        name
        (str/replace "-" "_"))))

(defn external-key->namespace-key [s]
  (when s
    (let [s-str (if (keyword? s)
                  (name s)
                  s)]
      (when (string? s-str)
        (keyword "tmdb" (str/replace s-str "_" "-"))))))

(defn- build-query-params [params]
  (->> params
       (map (fn [[k v]] [(namespace-key->external-key k) v]))
       (filter first)
       (into {})))

(defn build-request [endpoint params]
  (let [api-key (some-> params
                        :tmdb/api-key
                        (or "")
                        (str/replace #"\"" ""))
        query-params (build-query-params params)]
    {:http-request/method :http-method/get
     :http-request/url (str base-url endpoint)
     :http-request/query-params query-params
     :http-request/headers {"Authorization" (str "Bearer " api-key)}}))

(defn- convert-keys-recursively [data key-fn]
  (cond
    (nil? data) nil

    (map? data)
    (->> data
         (map (fn [[k v]] [(key-fn k) (convert-keys-recursively v key-fn)]))
         (into {}))

    (sequential? data)
    (mapv #(convert-keys-recursively % key-fn) data)

    :else data))

(def ^:private empty-response
  {:tmdb/results []
   :tmdb/page 0
   :tmdb/total-pages 0
   :tmdb/total-results 0})

(defn- parse-json [body]
  (try
    (when (string? body)
      (-> body
          js/JSON.parse
          (js->clj :keywordize-keys true)))
    (catch :default _e
      nil)))

(defn map-response [response]
  (if (:http-response/ok? response)
    (let [body (:http-response/body response)
          parsed (or (parse-json body) body)]
      (if parsed
        (convert-keys-recursively parsed external-key->namespace-key)
        (assoc empty-response
               :tmdb/error "Error parsing response"
               :http-response/body body)))
    (assoc empty-response
           :tmdb/error (str "HTTP request failed: " (:http-response/status response))
           :http-response/body (:http-response/body response))))
