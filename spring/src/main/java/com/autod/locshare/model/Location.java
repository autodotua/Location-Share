//package com.autod.locshare.model;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;
//
//@Entity
//@Table(name = "location")
//public class Location {
//
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private long id;
//  @Column(name = "groupName")
//  private String groupName;
//  @Column(name = "userId")
//  private String userId;
//  @Column(name = "latitude")
//  private double latitude;
//  @Column(name = "longitude")
//  private double longitude;
//  @Column(name = "altitude")
//  private double altitude;
//  @Column(name = "time")
//  private java.sql.Timestamp time;
//
//
//  public long getId() {
//    return id;
//  }
//
//  public void setId(long id) {
//    this.id = id;
//  }
//
//
//  public String getGroupName() {
//    return groupName;
//  }
//
//  public void setGroupName(String groupName) {
//    this.groupName = groupName;
//  }
//
//
//  public String getUser() {
//    return userId;
//  }
//
//  public void setUser(String userId) {
//    this.userId = userId;
//  }
//
//
//  public double getLatitude() {
//    return latitude;
//  }
//
//  public void setLatitude(double latitude) {
//    this.latitude = latitude;
//  }
//
//
//  public double getLongitude() {
//    return longitude;
//  }
//
//  public void setLongitude(double longitude) {
//    this.longitude = longitude;
//  }
//
//
//  public double getAltitude() {
//    return altitude;
//  }
//
//  public void setAltitude(double altitude) {
//    this.altitude = altitude;
//  }
//
//
//  public java.sql.Timestamp getTime() {
//    return time;
//  }
//
//  public void setTime(java.sql.Timestamp time) {
//    this.time = time;
//  }
//
//}
package com.autod.locshare.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "location")
public class Location {
    private int id;
    private String groupName;
    private String userId;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Timestamp time;

    @JsonInclude
    @Transient
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String userName;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @JsonInclude
    @Transient
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupId) {
        this.groupName = groupId;
    }

    @Basic
    @Column(name = "user_id")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "latitude")
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Basic
    @Column(name = "longitude")
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Basic
    @Column(name = "altitude")
    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    @Basic
    @Column(name = "time")
    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Location that = (Location) o;
        return id == that.id &&
                Objects.equals(groupName, that.groupName) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(latitude, that.latitude) &&
                Objects.equals(longitude, that.longitude) &&
                Objects.equals(altitude, that.altitude) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupName, userId, latitude, longitude, altitude, time);
    }
}
