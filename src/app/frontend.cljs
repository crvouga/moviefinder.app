(ns app.frontend
  (:require [app.frontend.store :as store]
            [app.media.frontend]
            [app.home.frontend]
            [app.auth.frontend]
            [app.rpc.frontend]
            [app.profile.frontend]
            [app.frontend.db]
            [app.frontend.sleep]
            [app.frontend.toaster :as toaster]
            [app.frontend.screen :as screen]))


(defn view [i]
  [:div {:class "fixed left-1/2 top-1/2 flex h-[100dvh] w-screen -translate-x-1/2 -translate-y-1/2 items-center justify-center overflow-hidden bg-black text-white"}
   [:div {:class "relative flex h-full max-h-[915px] w-full max-w-[520px] flex-col items-center justify-center overflow-hidden rounded min-[520px]:border border-neutral-500"}
    (toaster/view i)
    (screen/view i)]])

(defn -main []
  (store/initialize! view))
