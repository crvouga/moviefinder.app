(ns lib.fs.inter)

(defmulti read-file! (fn [fs _path] (:fs/impl fs)))

(defmulti write-file! (fn [fs _path _content] (:fs/impl fs)))

(defmulti delete-file! (fn [fs _path] (:fs/impl fs)))

(defmulti file-exists? (fn [fs _path] (:fs/impl fs)))
