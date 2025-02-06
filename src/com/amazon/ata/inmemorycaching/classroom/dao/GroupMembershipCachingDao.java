package com.amazon.ata.inmemorycaching.classroom.dao;

import com.amazon.ata.inmemorycaching.classroom.dao.models.GroupMembershipCacheKey;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

//this will manage the calls to the cache manager
//if there is  miss in the cache it will use the original DAO from the database
// if there is a hit then return  the data from the cache

//A cache needs a chache key to indention entries in the cahses
// in this ecample the cache key is the userId and the groupId

// since we have mulitiple values we want tto treat as a single unit
// we need a class to hold and manage the values

// we will be using the google Guava cahe manager
// we will be inserting calls to this CAching DAO between the application and the data base
//     so we need to be sure we mimic the behavior of the original DAO(same parameters, same return)
public class GroupMembershipCachingDao {

    //Define the cache for the cache manager to use
    // guava uses a LoadingCache object  for its cache

    LoadingCache<GroupMembershipCacheKey, Boolean> theCache;

// constructor must substantiate the cache and assign the reference
    @Inject
    public GroupMembershipCachingDao(final GroupMembershipDao delegateDao) {
        this.theCache = CacheBuilder.newBuilder()
                                    .maximumSize(20000)
                                    .expireAfterWrite(3, TimeUnit.HOURS)
                                    .build(CacheLoader.from(delegateDao::isUserInGroup)); // go build the cache with tehh DelegateDAO method
                                                // the delegateDAo must be named whatever comes after :: that receives a cache-key parameter
    }

    public boolean isUserInGroup(String userId, String groupId) {

        // using the getUncheck() to call the cache manager to avoid dealing with possible
        return theCache.getUnchecked(new GroupMembershipCacheKey(userId, groupId));
    }

}
