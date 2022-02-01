using LocShare.Models;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace LocShare.Dto
{
    public class Request<T>
    {
        [JsonProperty("data")]
        public T Data { get; set; }
    }
}