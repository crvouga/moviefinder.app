(ns linkpage.core.http-server.http-request
  (:require [clojure.core.async :refer [go chan close! <! put!]]
            [clojure.edn :as edn]))


(defn body-text-chan [^js req]
  (let [body-chan (chan)]
    (.on req "data" #(put! body-chan %))
    (.on req "end" #(close! body-chan))
    (go
      (let [chunks (<! body-chan)
            body (apply str chunks)]
        body))))

(defn body-edn-chan [^js req]
  (go
    (try
      (let [body-text (<! (body-text-chan req))
            body-edn  (edn/read-string body-text)]
        body-edn)
      (catch :default e
        (js/console.error "Failed to parse EDN:" e)
        nil))))