# R8 rules for the release build. The default optimize rules cover Android; the
# game itself uses no reflection and loads its one data file (the maze) as a Java
# resource, which R8 leaves alone — so there is nothing to keep yet.
