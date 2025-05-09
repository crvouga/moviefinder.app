
(ns app.feed.edit.frontend
  (:require
   [app.frontend.mod :as mod]
   [app.media.media-db.frontend]
   [app.frontend.screen :as screen]))


;; 
;; 
;; 
;; 
;; 

(defn- logic [_])

;; 
;; 
;; 
;; 

(defn- view [i]
  [screen/view-screen
   i :screen/feed-edit
   [:div "edit"]])

(mod/reg {:mod/name ::mod
          :mod/view view
          :mod/logic logic})