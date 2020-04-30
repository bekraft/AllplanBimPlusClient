# BIM+ API Client
or Scala drives Nemetschek Allplan's BIM+ API 

## Purpose

The [Nemetschek BIM+ platform](http://portal.bimplus.net) provides IFC model storage in conjunction with meta data administration services.

Unfortunately, there's no out-of-the-box command line interface (CLI) which is able to bundle a functional interface for repetitive tasks.  

The scope of this CLI is focused on IO of attribute meta data schemes (i.e. creating attributes, providing templates etc.) 

## Usage

First download Java and SBT. Install both. Afterwards run
```
cd [bimplus download directory]
sbt console
```

Login into either production API (https://api.bimplus.net) or stage API (https://api-stage.bimplus.net)

```
scala> Api.<prod | stage>.login("<my user login>","<my secret password>")
```

The application context will receive the access token once finished.

### Implemented services
#### Attributes service

```
>scala Api.<prod | stage>.attributes.<command>
```

Commands:
 - get - Detailed information of an attribute
 - getAll - Short listing of attributes
 - getAllComplete - Detailed listing of attributes
 - create - Create an attribute
 - createMany - Create multiple attributes sequentially
 - update - Update an attribute
 - delete - Delete an attribute
 - deleteMany - Delete multiple attributes sequentially

#### Attribute group service

```
>scala Api.<prod | stage>.attributeGroups.<command>
```

Commands:
 - get - Detailed information of an attribute group
 - getAll - Short listing of attribute groups
 - attributesOf - Scoping attribute service based on given group
 - create - Create an attribute group
 - update - Update an attribute group
 - delete - Delete an attribute group

#### Attribute template service

```
>scala Api.<prod | stage>.attributeTemplates.<command>
```

Commands:
 - get - Detailed information of an attribute template
 - getAll - Short listing of attribute templates
 - create - Create an attribute template
 - update - Update an attribute template
 - delete - Delete an attribute template

#### Project attribute template service

```
>scala Api.<prod | stage>.attributeProjectTemplates.<command>
```

Commands:
 - get - Detailed information of an attribute template
 - getAll - Short listing of attribute templates
 - create - Create an attribute template
 - update - Update an attribute template
 - delete - Delete an attribute template

 