package com.coupa.cloud.operations.ce.sand;


import java.util.Map;


public interface TimedCache<K, V> extends Map<K, V> {

  public boolean renewKey(K key);


  public V put(K key, V value, long lifeTimeMillis);

}
