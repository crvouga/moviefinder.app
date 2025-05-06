
(ns app.feed.edit.frontend
  (:require
   [app.frontend.mod :as mod]
   [app.media.media-db.frontend]))


;; 
;; 
;; 
;; 
;; 

(defn- logic [i]
  (println "logic" i))

;; 
;; 
;; 
;; 

(defn- view [_]
  [:div "edit"])

(mod/reg {:mod/name ::mod
          :mod/view view
          :mod/logic logic})