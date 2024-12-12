def toUpperFirst(s):
    if len(s) > 0:
        return s[0].upper() + s[1:]
    else:
        return s
def isWeakCommit(tags, weakTags):
    return len(list(filter(lambda tag: tag not in weakTags, tags))) == 0