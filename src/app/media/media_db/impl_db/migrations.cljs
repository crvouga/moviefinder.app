(ns app.media.media-db.impl-db.migrations)

(def migrations
  [{:create-table [:media :if-not-exists]
    :with-columns [[:id :text :primary-key]
                   [:tmdb-id :text]
                   [:title :text]
                   [:release-date :text]
                   [:overview :text]
                   [:poster-path :text]
                   [:vote-average :real]
                   [:vote-count :integer]
                   [:popularity :real]
                   [:poster-url :text]
                   [:backdrop-url :text]
                   [:created-at-posix :integer]
                   [:created-by-user-id :text]
                   [:updated-at-posix :integer]
                   [:updated-by-user-id :text]]}])