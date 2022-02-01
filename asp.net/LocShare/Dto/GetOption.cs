using LocShare.Models;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace LocShare.Dto
{
    public class GetOption
    {
        /// <summary>
        /// 获取多少时间之后的记录，秒为单位
        /// </summary>
        [JsonProperty("time")]
        public int Time { get; set; }
    }
}