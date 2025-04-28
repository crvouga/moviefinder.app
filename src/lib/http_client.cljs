(ns lib.http-client
  (:require
   [clojure.core.async :refer [>! chan close! go]]))

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
                     {:http-response/ok? (.-ok response)
                      :http-response/status (.-status response)
                      :http-response/headers (js-headers->clj (.-headers response))
                      :http-response/body (if (string? data) data (js->clj data :keywordize-keys true))})
                 (close! response-chan))))))

(defn on-error-response [error response-chan]
  (go
    (>! response-chan {:http-response/ok? false
                       :http-response/error (str error)})
    (close! response-chan)))

(defn fetch-chan!
  "Sends an HTTP request using the Fetch API.
   Accepts an `http-request` map with:
   - :http-request/method (:http-method/get, :http-method/post, :http-method/put, :http-method/delete, etc.)
   - :http-request/url (the endpoint)
   - :http-request/headers (optional, a map of headers)
   - :http-request/body (optional, the request body as a string or FormData for POST/PUT requests)
   Returns a channel containing the HTTP response map."
  [http-request]
  #_(println "http-request" http-request)
  (let [response-chan (chan)
        {:http-request/keys [method url headers body]} http-request
        init (cond-> {:method (-> method name .toUpperCase)}
               headers (assoc :headers (clj->js headers))
               body (assoc :body body))]
    (-> (js/fetch url (clj->js init))
        (.then #(on-success-response % response-chan))
        (.catch #(on-error-response % response-chan)))
    response-chan))
