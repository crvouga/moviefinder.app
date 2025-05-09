(ns lib.http-server.http-res
  (:require
   [lib.http-cookie :as http-cookie]))

(defn set-header! [^js res key value]
  (.setHeader res key value))

(defn end!
  ([^js res] (.end res))
  ([^js res body] (.end res body)))

(defn set-cookie! [^js res cookie]
  (set-header! res "Set-Cookie" (http-cookie/to-header-value cookie)))
