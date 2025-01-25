(ns core.http-server.http-response)

(defn set-header! [^js res key value]
  (.setHeader res key value))

(defn end!
  ([^js res] (.end res))
  ([^js res body] (.end res body)))