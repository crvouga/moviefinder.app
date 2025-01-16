(ns linkpage.auth.login.frontend.shared)

(defn view-layout [title body]
  [:main.container {:style {:padding-top "4rem"}}
   [:header [:h1 title]]
   [:section body]])