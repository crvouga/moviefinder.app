(ns linkpage.core.http-server
  (:require ["http" :as http]
            [clojure.core.async :refer [go chan close! <!]]))

(defn new! [request-handler!]
  (let [http-server! (.createServer http request-handler!)]
    http-server!))

(defn listen! [^js http-server! port]
  (go
    (let [started-chan (chan)]
      (.listen http-server! port #(close! started-chan))
      (<! started-chan))))

