﻿using Newtonsoft.Json;

namespace LocShare.Dto
{
    public class Response<T>
    {
        [JsonProperty("data")]
        public T Data { get; set; }

        [JsonProperty("message")]
        public string Message { get; set; }

        [JsonProperty("succeed")]
        public bool Succeed { get; set; } = true;
    }
}