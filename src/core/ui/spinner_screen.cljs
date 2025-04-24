(ns core.ui.spinner-screen
  (:require
   [core.ui.icon :as icon]))

(defn view []
  [:div.w-full.flex-1.grid.place-items-center
   [icon/spinner {:class "size-20 animate-spin"}]])