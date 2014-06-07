(ns lazy-server.parameter-decoder)

(def conversions
  {"%20" \space
   "%21" \!
   "%22" \"
   "%3C" \<
   "%3E" \>
   "%3D" \=
   "%3B" \;
   "%2B" \+
   "%2D" \-
   "%2A" \*
   "%26" \&
   "%40" \@
   "%23" \#
   "%24" \$
   "%5B" \[
   "%5D" \]
   "%3A" \:
   "%3F" \?
   "%2E" \.
   "%2C" \,
   "%7B" \{
   "%7D" \} })

(defn decode [params]
  (loop [output []
         input params]
    (if (= (count input) 0)
      (apply str output)
      (let [[before-split after-split] (split-with #(not= \% %) input)]
        (recur
          (conj
            (into output before-split)
            (conversions (apply str (take 3 after-split))))
          (drop 3 after-split))))))
