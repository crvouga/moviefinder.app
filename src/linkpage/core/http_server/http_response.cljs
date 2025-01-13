(ns linkpage.core.http-server.http-response)

(defn end! [^js res]
  (.end res))