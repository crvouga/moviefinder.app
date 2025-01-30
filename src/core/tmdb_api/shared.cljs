(ns core.tmdb-api.shared
  (:require [clojure.string :as str]
            [core.map-ext :as map-ext]
            [core.json :as json]))


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

(defn ->api-key [params]
  (some-> params :tmdb/api-key (or "") (str/replace #"\"" "")))

(defn build-request [endpoint params]
  (let [api-key (->api-key params)
        query-params (build-query-params params)]
    {:http-request/method :http-method/get
     :http-request/url (str base-url endpoint)
     :http-request/query-params query-params
     :http-request/headers {"Authorization" (str "Bearer " api-key)}}))

(def ^:private empty-response
  {:tmdb/results []
   :tmdb/page 0
   :tmdb/total-pages 0
   :tmdb/total-results 0})

(defn- map-response-ok [response]
  (let [body (:http-response/body response)
        parsed (or (json/json->clj body) body)]
    (if parsed
      (map-ext/map-keys-recursively parsed external-key->namespace-key)
      (assoc empty-response
             :tmdb/error "Error parsing response"
             :http-response/body body))))

(defn- map-response-error [response]
  (assoc empty-response
         :tmdb/error (str "HTTP request failed: " (:http-response/status response))
         :http-response/body (:http-response/body response)))

(defn map-response [response]
  (if (:http-response/ok? response)
    (map-response-ok response)
    (map-response-error response)))
          