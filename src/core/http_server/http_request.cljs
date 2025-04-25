(ns core.http-server.http-request
  (:require
   [clojure.core.async :refer [<! chan close! go go-loop put!]]
   [clojure.edn :as edn]))



(defn url
  "Returns the URL of the HTTP request without search parameters."
  [^js req]
  (let [full-url (.-url req)]
    (if-let [question-mark-idx (.indexOf full-url "?")]
      (.substring full-url 0 question-mark-idx)
      full-url)))

(defn body-binary-chan
  "Collects binary chunks from the HTTP request into a single binary sequence."
  [^js req]
  (let [body-chan (chan)]
    (.on req "data" #(put! body-chan %))
    (.on req "end" #(close! body-chan))
    (go-loop [chunks []]
      (if-let [chunk (<! body-chan)]
        (recur (conj chunks chunk))
        (.toString (js/Buffer.concat (clj->js chunks)))))))

(defn body-text-chan
  "Converts the binary body of the request into a text string."
  [^js req]
  (go
    (try
      (<! (body-binary-chan req))
      (catch :default e
        (js/console.error "Failed to read body text:" e)
        nil))))

(defn body-edn-chan
  "Parses the request body as EDN."
  [^js req]
  (go
    (try
      (let [body-text (<! (body-text-chan req))]
        (edn/read-string body-text))
      (catch :default e
        (js/console.error "Failed to parse EDN:" e)
        nil))))
