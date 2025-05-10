(ns lib.http-client
  (:require
   [clojure.core.async :refer [>! chan close! go]]
   [clojure.string :as s]))

(defn to-map-keyword [k] (if (string? k) (keyword k) k))
(defn to-map-entry [[k v]] [(to-map-keyword k) v])
(defn tuples-to-map [tuples] (->> tuples (map to-map-entry) (into {})))

(defn js-headers->clj [headers]
  (-> headers
      .entries
      js/Array.from
      (js->clj :keywordize-keys true)
      tuples-to-map))

(defn parse-response-body [response]
  (let [content-type (-> response .-headers (.get "content-type"))]
    (cond
      (and content-type (re-find #"application/json" content-type))
      (.json response)

      :else
      (.text response))))

(defn on-success-response [response response-chan]
  (-> (parse-response-body response)
      (.then (fn [data]
               (go
                 (>! response-chan
                     {:http/ok? (.-ok response)
                      :http/status (.-status response)
                      :http/headers (js-headers->clj (.-headers response))
                      :http/body (if (string? data) data (js->clj data :keywordize-keys true))})
                 (close! response-chan))))))

(defn on-error-response [error response-chan]
  (go
    (>! response-chan {:http/ok? false
                       :http-res/error (str error)})
    (close! response-chan)))

(defn query-params->string [query-params]
  (s/join "&" (map (fn [[k v]] (str (name k) "=" (js/encodeURIComponent v))) query-params)))


(defn to-url [http-req]
  (let [url (:http/url http-req)
        query-params (:http/query-params http-req)]
    (if query-params
      (str url "?" (query-params->string query-params))
      url)))

(defn fetch!
  "Sends an HTTP request using the Fetch API.
   Accepts an `http-req` map with:
   - :http/method (:http/get, :http/post, :http/put, :http/delete, etc.)
   - :http/url (the endpoint)
   - :http/headers (optional, a map of headers)
   - :http/body (optional, the request body as a string or FormData for POST/PUT requests)
   - :http/credentials (optional, include credentials mode: :include, :same-origin, or :omit)
   - :http/query-params (optional, a map of query parameters)
   Returns a channel containing the HTTP response map."
  [http-req]
  (let [response-chan (chan)
        {:keys [http/method http/headers http/body http/credentials]} http-req
        url-with-params (to-url http-req)
        init (cond-> {:method (-> method name .toUpperCase)
                      :credentials (or (some-> credentials name) "same-origin")}
               headers (assoc :headers (clj->js headers))
               body (assoc :body body))]
    (-> (js/fetch url-with-params (clj->js init))
        (.then #(on-success-response % response-chan))
        (.catch #(on-error-response % response-chan)))
    response-chan))
