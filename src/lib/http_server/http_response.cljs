(ns lib.http-server.http-response)

(defn set-header! [^js res key value]
  (.setHeader res key value))

(defn allow-cors! [^js res]
  (set-header! res "Access-Control-Allow-Origin" "*")
  (set-header! res "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS")
  (set-header! res "Access-Control-Allow-Headers" "Content-Type, Authorization"))

(defn end!
  ([^js res] (.end res))
  ([^js res body] (.end res body)))