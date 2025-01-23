(ns moviefinder-app.list.frontend)


#_(defn query-my-liked-movies [input]
    {:select [:movie/id :movie/title :movie/year :movie/plot :movie/poster]
     :from [:movie]
     :join [:movie/like]
     :where [:=  (:user/id input)]})

#_(store/sub!
   ::lists
   (fn [i]
     {:select [:movie/id :movie/title :movie/year :movie/plot :movie/poster]
      :from [:movie]
      :join [:movie/like]
      :where [:=  (:user/id i)]}))

#_(defn view [i]
    (let [lists (-> i :store/state ::lists)]
      [:div]))