using LocShare.Models;
using Newtonsoft.Json;
using System;
using System.ComponentModel.DataAnnotations.Schema;

namespace LocShare.Dto
{
    public class UserDto
    {

        [JsonProperty("name")]
        public string Name { get; set; }


        [JsonProperty("displayName")]
        public string DisplayName { get; set; }

        [JsonProperty("password")]
        public string Password { get; set; }

        [JsonProperty("groupName")]
        public string GroupName { get; set; } = "";

        [JsonProperty("lastUpdateTime")]
        public DateTime? LastUpdateTime { get; set; }

        [JsonProperty("last_location_id")]
        public int? LastLocationId { get; set; }
        [JsonProperty("lastLocation")]
        public LocationDto LastLocation { get; set; }

        [JsonProperty("token")]
        public string Token { get; set; }

        public override string ToString()
        {
            return Name + "（" + DisplayName + "） - " + GroupName;
        }
    }
}