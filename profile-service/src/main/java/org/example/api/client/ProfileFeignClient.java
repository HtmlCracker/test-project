package org.example.api.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "profileFeignClient", url = "")
public class ProfileFeignClient {
}
