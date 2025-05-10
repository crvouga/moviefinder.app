(ns lib.tmdb-api.shared
  (:require
   [clojure.string :as str]
   [lib.json :as json]
   [lib.map-ext :as map-ext]))


(def base-url "https://api.themoviedb.org/3")

(def page-size 20)

(def movie-id-fight-club (str 550))

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

(defn namespaced-key? [k]
  (str/includes? (str k) "tmdb/"))

(defn- to-query-params [params]
  (->> params
       (filter (fn [[k _]] (namespaced-key? k)))
       (map (fn [[k v]] [(namespace-key->external-key k) v]))
       (filter first)
       (into {})))

(defn to-api-key [params]
  (some-> params :tmdb/api-key (or "") (str/replace #"\"" "")))

(defn build-request [params endpoint]
  (let [api-key (to-api-key params)
        query-params (to-query-params params)]
    {:http/method :http/get
     :http/url (str base-url endpoint)
     :http/query-params query-params
     :http/headers {"Authorization" (str "Bearer " api-key)}}))

(defn- map-response-ok [response empty-response]
  (let [body (:http/body response)
        parsed (or (json/json->clj body) body)]
    (if parsed
      (map-ext/map-keys-recursively parsed external-key->namespace-key)
      (when empty-response
        (assoc empty-response
               :tmdb/error "Error parsing response"
               :http/body body)))))

(defn- map-response-error [response empty-response]
  (when empty-response
    (assoc empty-response
           :tmdb/error (str "HTTP request failed: " (:http/status response))
           :http/body (:http/body response))))

(defn map-response
  ([response]
   (map-response response {}))
  ([response empty-response]
   (if (:http/ok? response)
     (map-response-ok response empty-response)
     (map-response-error response empty-response))))