(ns moviefinder-app.home.frontend
  (:require
   [moviefinder-app.frontend.db-conn-sql :refer [db-conn]]
   [moviefinder-app.frontend.screen :as screen]
   [moviefinder-app.core.db-conn-sql.interface :as db-conn-sql]
   [moviefinder-app.frontend.ui.top-bar :as top-bar]))



(def movies!
  (db-conn-sql/watch
   db-conn
   [:select [:media/title :media/year :media/genre-ids]
    :from :media
    :order-by [:media/popularity :desc]]))

(screen/reg!
 :screen/home
 (fn [_i]
   [:div.w-full.flex-1.flex.flex-col
    [top-bar/view {:top-bar/title "Home"}]
    [:div "hello"]]))