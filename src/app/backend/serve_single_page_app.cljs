(ns app.backend.serve-single-page-app
  (:require ["fs" :as fs]
            ["path" :as path]
            [app.backend.http-respond :refer [http-respond!]]))

(def public-dir (path/resolve "public"))

(defn- get-content-type [file-path]
  (let [ext (.toLowerCase (.extname path file-path))]
    (case ext
      ".map" "application/javascript"
      ".html" "text/html"
      ".css" "text/css"
      ".js" "application/javascript"
      ".json" "application/json"
      ".png" "image/png"
      ".jpg" "image/jpeg"
      ".gif" "image/gif"
      "application/octet-stream")))

(defn- end! [^js res status content-type data]
  (.writeHead res status #js {"Content-Type" content-type})
  (.end res data))

(defn- serve-file-err! [res err]
  (println "Error serving file" err)
  (let [index-path (path/join public-dir "index.html")]
    (fs/readFile
     index-path
     "utf8"
     (fn [err data]
       (if err
         (end! res 500 "text/plain" "500 Internal Server Error")
         (end! res 200 "text/html" data))))))

(defn- serve-file-ok! [res file-path]
  #_(println "Serving file" file-path)
  (fs/readFile
   file-path
   (fn [err data]
     (if err
       (end! res 500 "text/plain" "500 Internal Server Error")
       (end! res 200 (get-content-type file-path) data)))))

(defn- serve-file! [file-path res]
  (fs/access file-path fs/constants.F_OK
             (fn [err]
               (if err
                 (serve-file-err! res err)
                 (serve-file-ok! res file-path)))))

(defmethod http-respond! :default [req res]
  (let [url-path (-> req .-url)
        safe-path (if (= url-path "/") "index.html" url-path)
        file-path (path/join public-dir safe-path)]
    #_(println "Serving file" file-path)
    (serve-file! file-path res)))