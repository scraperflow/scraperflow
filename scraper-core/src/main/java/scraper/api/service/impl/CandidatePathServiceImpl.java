package scraper.api.service.impl;


import org.slf4j.Logger;
import scraper.api.service.CandidatePathService;

import java.util.HashSet;
import java.util.Set;

public class CandidatePathServiceImpl implements CandidatePathService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CandidatePathServiceImpl.class);
    private final Set<String> knownPaths = new HashSet<>();


    @Override
    public void addPath(String path) {
        knownPaths.add(path);
    }

    @Override
    public Set<String> getCandidatePaths() {
        return knownPaths;
    }
}
