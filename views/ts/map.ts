import ol, { color, Feature, tilegrid, geom, proj } from "openlayers";
import $ from "jquery"
import { Location, User } from "./common";
import { postUpdate } from "./net";
let Select = ol.interaction.Select
let Overlay = ol.Overlay

export class OpenLayersMap {

    private olMap: ol.Map;

    private overlay: ol.Overlay;
    private selection: ol.interaction.Select;
    private peopleSource: ol.source.Vector;
    private selfSource: ol.source.Vector;
    private peopleLayer: ol.layer.Vector;
    constructor() {
        // const center = proj.fromLonLat([121, 29]);
        // console.log('center is:', center);
        let tile1 = new ol.layer.Tile({
            // source: new ol.source.OSM({}),
            source: new ol.source.XYZ({
                url: 'https://t0.tianditu.com/vec_w/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=vec&style=default&TILEMATRIXSET=w&format=tiles&height=256&width=256&tilematrix={z}&tilerow={y}&tilecol={x}&tk=9396357d4b92e8e197eafa646c3c541d'
            }),
        });

        let tile2 = new ol.layer.Tile({
            // source: new ol.source.OSM({}),
            source: new ol.source.XYZ({
                url: 'https://t0.tianditu.com/cva_w/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=cva&style=default&TILEMATRIXSET=w&format=tiles&height=256&width=256&tilematrix={z}&tilerow={y}&tilecol={x}&tk=9396357d4b92e8e197eafa646c3c541d'
            }),
        });
        this.peopleSource = new ol.source.Vector({
            wrapX: false
        });
        let style = new ol.style.Style({
            image: new ol.style.Circle({
                radius: 10,
                fill: new ol.style.Fill({
                    color: '#f6fcff'
                }),
                stroke: new ol.style.Stroke({
                    color: '#2fbbba',
                    width: 5
                }),
            }),
            text: new ol.style.Text({
                offsetY: 24,
                scale: 2,
                fill: new ol.style.Fill({
                    color: '#000'
                }),
                stroke: new ol.style.Stroke({
                    color: '#fff',
                    width: 3
                })
            })//text
        });
        this.peopleLayer = new ol.layer.Vector({
            source: this.peopleSource,
            style: function (feature) {
                style.getText().setText(feature.get("userDisplayName"))
                return [style]
            }
        })//layer

        this.selfSource = new ol.source.Vector();
        let selfLayer = new ol.layer.Vector({
            source: this.selfSource,
            style: new ol.style.Style({
                image: new ol.style.Circle({
                    radius: 6,
                    fill: new ol.style.Fill({
                        color: '#f6fcff'
                    }),
                    stroke: new ol.style.Stroke({
                        color: '#2962FF',
                        width: 5
                    })
                })
            })
        })


        this.olMap = new ol.Map({
            // 设置显示地图的视图
            view: new ol.View({
                center: proj.fromLonLat([104, 28]),
                zoom: 5,
                // center: center,
                // zoom: 14
            }),
            target: 'map-view',
            layers: [tile1, tile2, this.peopleLayer, selfLayer]
        });

        let selectionStyle = new ol.style.Style({
            image: new ol.style.Circle({
                radius: 10,
                fill: new ol.style.Fill({
                    color: '#f6fcff'
                }),
                stroke: new ol.style.Stroke({
                    color: '#FF4081',
                    width: 8
                })
            })
        });

        // this.map.on("click", this.mapClick);
        this.selection = new Select({
            condition: ol.events.condition.click,
            style: selectionStyle
        });
        this.olMap.addInteraction(this.selection);
        this.selection.on('select', e => { this.onMapClick(this, e); });
        $('#popup-closer').click(() => {
            this.closePopup();
        });
        this.overlay = new Overlay({
            element: $("#popup")[0],
            autoPan: true,
            autoPanAnimation: {
                source: [0, 0],
                duration: 250
            }
        });

        this.olMap.addOverlay(this.overlay);

        if (localStorage.getItem("positionning") == "false") {
            $("#nav-positionning").text("开启定位")
        }
        else {
            this.setPositionning(true);
        }
    }
    private watchId: number | null = null;
    public setPositionning(on: Boolean) {
        if (on) {
            console.log("开启定位");
            
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(p => this.selfPositionUpdate(p, true), this.showPositioningError);
                this.watchId = navigator.geolocation.watchPosition(p => this.selfPositionUpdate(p), this.showPositioningError);
            }
            else {
                $("#float-alert-msg").text("该设备或浏览器不支持定位");
            }
        }
        else {
            this.watchId = null;
            this.selfSource.clear();
            if (navigator.geolocation) {
                navigator.geolocation.clearWatch(this.watchId!)
            }
        }
    }
    private closePopup() {
        this.overlay.setPosition(undefined);
        if (this.popupInterval != null) {
            clearInterval(this.popupInterval);
            this.popupInterval = null
        }
    }

    private onMapClick(cls: OpenLayersMap, e: any) {
        let feature: ol.Feature = e.selected[0]
        this.showPopup(feature);
    }

    public select(username: string) {
        this.peopleSource.forEachFeature(feature => {
            if (feature.get("username") == username) {
                this.showPopup(feature);
                return;
            }
        })
    }
    private popupInterval: any | null = null;
    private showPopup(feature: ol.Feature) {
        if (feature === undefined) {
            return;
        }
        if (this.popupInterval != null) {
            clearInterval(this.popupInterval);
            this.popupInterval = null
        }
        let point = <ol.geom.Point>feature.getGeometry();
        let pos = point.getFirstCoordinate();
        this.selected = feature;
        $("#popup-name").text(feature.get("userDisplayName"));

        let secondsBefore = (Date.now() - feature.get("time")) / 1000;


        $("#popup-last-time").html(secondsBefore.toFixed(0) + "秒前<br>" + new Date(feature.get("time")).toLocaleTimeString());
        this.popupInterval = setInterval(() => {
            let secondsBefore = (Date.now() - feature.get("time")) / 1000;
            $("#popup-last-time").html(secondsBefore.toFixed(0) + "秒前<br>" + new Date(feature.get("time")).toLocaleTimeString());

        }, 1000);
        $("#popup-lat").text(feature.get("lat") + "°");
        $("#popup-lng").text(feature.get("lng") + "°");
        if (feature.get("alt")) {
            $("#popup-alt").text(feature.get("alt") + "m");
        }
        else {
            $("#popup-alt").text("未知");
        }
        if (feature.get("acc")) {
            $("#popup-acc").text(feature.get("acc") + "m");
        }
        else {
            $("#popup-acc").text("未知");
        }
        this.overlay.setPosition(pos);
        this.to(pos);
    }

    public selected: Feature | null = null;

    public to(loc: [number, number], zoom?: number): void {

        if (zoom) {
            if (loc[0] + loc[1] < 1000) {
                this.olMap.getView().animate({ center: ol.proj.fromLonLat(loc), zoom: zoom, duration: 2000 })
            }
            else {
                this.olMap.getView().animate({ center: loc, zoom: zoom, duration: 600 })
            }
        }
        else {
            if (loc[0] + loc[1] < 1000) {
                this.olMap.getView().animate({ center: ol.proj.fromLonLat(loc), duration: 2000 })
            }
            else {
                this.olMap.getView().animate({ center: loc, duration: 600 })
            }
        }
    }




    public remove(layer: any): void {
        this.olMap.removeLayer(layer);
    }
    public add(layer: any): void {
        if (layer instanceof ol.Overlay) {

            this.olMap.addOverlay(layer);
        }
        else {
            this.olMap.addLayer(layer);
        }
    }

    public update(users: User[]): void {
        // let deleteNeededFeatures:Feature[]= [];
        // this.peopleSource.forEachFeature(feature=>{
        //     if(people.filter(loc=>loc.id==feature.get("id")).length>0)
        //     {
        //         deleteNeededFeatures.push(feature);
        //     }
        // })
        // for (const feature of deleteNeededFeatures) {

        //     this.peopleSource.removeFeature(feature);
        // }

        this.peopleSource.clear();
        for (const user of users) {
            if (user.lastLocation == null) {
                continue;
            }
            let now = Date.now();
            if (now - new Date(user.lastLocation.time).getTime() > 1000 * 60 * 30 - new Date().getTimezoneOffset() * 60 * 1000) {
                continue;
            }

            let l = user.lastLocation
            let point = new geom.Point(proj.fromLonLat([l.longitude!, l.latitude!]));
            let feature = new Feature(point);
            feature.set("id", l.id);
            feature.set("userDisplayName", user.displayName ? user.displayName : user.name);
            feature.set("username", user.name);

            feature.set("time", new Date(new Date(l.time).getTime() - new Date().getTimezoneOffset() * 60 * 1000));
            feature.set("lat", l.latitude!.toFixed(6));
            feature.set("lng", l.longitude!.toFixed(6));
            if (l.altitude) {
                feature.set("alt", l.altitude.toFixed(2));
            }
            if (l.accuracy) {
                feature.set("acc", l.accuracy.toFixed(0));
            }
            this.peopleSource.addFeature(feature);
        }
    }
public lastPoint:[number,number]|null=null;
    private selfPositionUpdate(position: Position, to = false) {
        console.log("更新定位");
        
        this.selfSource.clear();
        let point = proj.fromLonLat([position.coords.longitude, position.coords.latitude]);
        let feature = new Feature(new geom.Point(point));
        feature.set("userDisplayName", "我");
        feature.set("time", new Date().getTime());
        feature.set("lat", position.coords.latitude.toFixed(6));
        feature.set("lng", position.coords.longitude.toFixed(6));
        if (position.coords.altitude) {
            feature.set("alt", position.coords.altitude.toFixed(2));
        }
        if (position.coords.accuracy) {
            feature.set("acc", position.coords.accuracy.toFixed(0));
        }
        this.selfSource.addFeature(feature);

        let location: Location = new Location();
        location.latitude = position.coords.latitude;
        location.longitude = position.coords.longitude;
        location.altitude = position.coords.altitude ? position.coords.altitude : undefined;
        location.accuracy = position.coords.accuracy ? position.coords.accuracy : undefined;
        location.speed = position.coords.speed ? position.coords.speed : undefined;
        postUpdate(location);
    this.    lastPoint=point;
        $("#float-alert-msg").hide();
        if (to) {
            this.to(point, 14)
        }
    }

    private showPositioningError(error: PositionError) {
        switch (error.code) {
            case error.PERMISSION_DENIED:
                $("#float-alert-msg").text("用户禁止了定位功能");
                break;
            case error.POSITION_UNAVAILABLE:
                $("#float-alert-msg").text("位置信息不可用");
                break;
            case error.TIMEOUT:
                $("#float-alert-msg").text("请求定位超时");
                break;
        }
        $("#float-alert-msg").show();
    }
}

