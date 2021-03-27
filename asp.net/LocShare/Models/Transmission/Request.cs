using LocShare.Models.Entity;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace LocShare.Models.Transmission
{
    public class Request<T>
    {
        [JsonProperty("data")]
        public T Data { get; set; }
        [JsonProperty("user")]
        public UserEntity User { get; set; }
    }
}