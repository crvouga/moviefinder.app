(ns lib.fs.impl-node
  (:require
   [lib.fs.inter :as fs]
   [clojure.core.async :as a]
   ["fs" :as node-fs]
   #_["path" :as path]))

(defmethod fs/read-file! :fs/node
  [_fs path]
  (let [c (a/chan)]
    (node-fs/readFile
     path #js {:encoding "utf8"}
     (fn [err data]
       (if err
         (a/put! c {:error err})
         (a/put! c data))
       (a/close! c)))
    c))

(defmethod fs/write-file! :fs/node
  [_fs path content]
  (let [c (a/chan)]
    (node-fs/writeFile
     path content
     (fn [err]
       (if err
         (a/put! c {:error err})
         (a/put! c true))
       (a/close! c)))
    c))

(defmethod fs/file-exists? :fs/node
  [_fs path]
  (let [c (a/chan)]
    (node-fs/access
     path
     (fn [err]
       (if err
         (a/put! c false)
         (a/put! c true))
       (a/close! c)))
    c))

(defmethod fs/delete-file! :fs/node
  [_fs path]
  (let [c (a/chan)]
    (node-fs/unlink
     path
     (fn [err]
       (if err
         (a/put! c {:error err})
         (a/put! c true))
       (a/close! c)))
    c))
