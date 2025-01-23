(ns moviefinder-app.home.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [moviefinder-app.core.ui.top-bar :as top-bar]
   [moviefinder-app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]))


#_(def movies!
    (db-conn-sql/watch
     db-conn
     [:select [:media/title :media/year :media/genre-ids]
      :from :media
      :order-by [:media/popularity :desc]]))

(screen/reg!
 :screen/home
 (fn [i]
   [:div.w-full.flex-1.flex.flex-col
    [top-bar/view {:top-bar/title "Home"}]
    [:div.w-full.flex-1 "hello"]
    [top-level-bottom-buttons/view i]]))
    