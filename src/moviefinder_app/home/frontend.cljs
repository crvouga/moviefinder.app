(ns moviefinder-app.home.frontend
  (:require
   [moviefinder-app.frontend.screen :as screen]
   [moviefinder-app.core.ui.top-bar :as top-bar]
   [moviefinder-app.frontend.ui.top-level-bottom-buttons :as top-level-bottom-buttons]
   [moviefinder-app.media.media-db.interface :as media-db]
   [moviefinder-app.frontend.ctx :refer [ctx]]))


#_(def movies!
    (sql/watch
     db-conn
     [:select [:media/title :media/year :media/genre-ids]
      :from :media
      :order-by [:media/popularity :desc]]))

(println "query=" (media-db/query  (merge ctx {:query/limit 10
                                               :query/offset 0})))

(screen/reg!
 :screen/home
 (fn [i]
   [:div.w-full.flex-1.flex.flex-col
    [top-bar/view {:top-bar/title "Home"}]
    [:div.w-full.flex-1 "hello"]
    [top-level-bottom-buttons/view i]]))
    