(ns linkpage.core.http-client
  (:require [cljs.core.async :refer [chan go >! close!]]))

(defn js-headers->clj [headers]
  (js->clj (.fromEntries (.entries headers)) :keywordize-keys true))

(defn js-body->clj [body]
  (js->clj body :keywordize-keys true))

(defn handle-success-response [response response-chan]
  (-> (.json response)
      (.then (fn [data]
               (go
                 (>! response-chan
                     {:http-response/status (.-status response)
                      :http-response/headers (js-headers->clj (.-headers response))
                      :http-response/body (js-body->clj data)})
                 (close! response-chan))))))

(defn handle-error-response [error response-chan]
  (go
    (>! response-chan {:http-response/error (str error)})
    (close! response-chan)))

(defn send!
  "Sends an HTTP request using the Fetch API.
   Accepts an `http-request` map with:
   - :http-request/method (:http-method/get, :http-method/post, :http-method/put, :http-method/delete, etc.)
   - :http-request/url (the endpoint)
   - :http-request/headers (optional, a map of headers)
   - :http-request/body (optional, the request body as a string or FormData for POST/PUT requests)
   Returns a channel containing the HTTP response map."
  [http-request]
  (let [response-chan (chan)
        {:http-request/keys [method url headers body]} http-request
        init (cond-> {:method (-> method name .toUpperCase)}
               headers (assoc :headers (clj->js headers))
               body (assoc :body body))]
    (-> (js/fetch url (clj->js init))
        (.then #(handle-success-response % response-chan))
        (.catch #(handle-error-response % response-chan)))
    response-chan))
