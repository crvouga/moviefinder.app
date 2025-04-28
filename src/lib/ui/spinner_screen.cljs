(ns lib.ui.spinner-screen
  (:require
   [lib.ui.icon :as icon]))

(defn view []
  [:div.w-full.flex-1.grid.place-items-center
   [icon/spinner {:class "size-20 animate-spin"}]])