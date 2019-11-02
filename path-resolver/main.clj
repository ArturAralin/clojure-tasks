; This is implementation of Node.js path.resolve(...args)
; Link: https://nodejs.org/docs/latest/api/path.html#path_path_resolve_paths

(require '[clojure.string :as str])

(defn path-resolve [sd & rest]
  (let [rp (atom (str/split sd #"/"))]
    (doseq [sub-path rest]
      (doseq [path-item (str/split sub-path #"/")]
        (reset! rp (cond
                     (= ".." path-item) (vec (drop-last @rp))
                     (= "" path-item) [""]
                     (= "." path-item) @rp
                     :else (conj @rp path-item)))))
    (str/join "/" @rp)))

(path-resolve "/part/of/path" "../other/part" "./here") ; => "/part/of/other/part/here"
