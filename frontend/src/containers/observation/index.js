/*
 *  MUSIT is a museum database to archive natural and cultural history data.
 *  Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License,
 *  or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import React from 'react'

import { Panel, Grid, Row, Col, FormGroup, Button, ControlLabel, SplitButton, MenuItem } from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import { connect } from 'react-redux'
import Language from '../../components/language'
import DatePicker from 'react-bootstrap-date-picker'
import Autosuggest from 'react-autosuggest'
import { observationTypeDefinitions, defineCommentType,
  defineFromToType, definePestType, defineStatusType } from './observationTypeDefinitions'
import { addObservation } from '../../reducers/observation'

// TODO: Bind finished page handling to redux and microservices.
const mapStateToProps = () => ({
  translate: (key, markdown) => Language.translate(key, markdown)
})

const mapDispatchToProps = (dispatch) => ({
  onSaveObservation: (data) => {
    dispatch(addObservation(data))
  }
})

@connect(mapStateToProps, mapDispatchToProps)
export default class ObservationView extends React.Component {
  static propTypes = {
    translate: React.PropTypes.func.isRequired,
    onSaveObservation: React.PropTypes.func.isRequired
  }

  constructor(props) {
    super(props)
    const { translate } = props

    this.observationTypeDefinitions = observationTypeDefinitions(translate, this.actions)
    // TODO: Language binding.
    // TODO: Action binding.

    this.actions = {
      changeTempFrom: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'temperature') {
            retVal = { ...o, data: { ...o.data, fromValue: v } }
          }
          return retVal
        }) })
      },
      changeTempTo: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'temperature') {
            retVal = { ...o, data: { ...o.data, toValue: v } }
          }
          return retVal
        }) })
      },
      changeTempComment: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'temperature') {
            retVal = { ...o, data: { ...o.data, commentValue: v } }
          }
          return retVal
        }) })
      },
      changeHypoxicAirFrom: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'hypoxicAir') {
            retVal = { ...o, data: { ...o.data, fromValue: v } }
          }
          return retVal
        }) })
      },
      changeHypoxicAirTo: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'hypoxicAir') {
            retVal = { ...o, data: { ...o.data, toValue: v } }
          }
          return retVal
        }) })
      },
      changeHypoxicAirComment: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'hypoxicAir') {
            retVal = { ...o, data: { ...o.data, commentValue: v } }
          }
          return retVal
        }) })
      },
      changeRHFrom: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'rh') {
            retVal = { ...o, data: { ...o.data, fromValue: v } }
          }
          return retVal
        }) })
      },
      changeRHTo: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'rh') {
            retVal = { ...o, data: { ...o.data, toValue: v } }
          }
          return retVal
        }) })
      },
      changeRHComment: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'rh') {
            retVal = { ...o, data: { ...o.data, commentValue: v } }
          }
          return retVal
        }) })
      },
      changeLuxLeft: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'lux') {
            retVal = { ...o, data: { ...o.data, leftValue: v } }
          }
          return retVal
        }) })
      },
      changeLuxRight: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'lux') {
            retVal = { ...o, data: { ...o.data, rightValue: v } }
          }
          return retVal
        }) })
      },
      changeGasLeft: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'gas') {
            retVal = { ...o, data: { ...o.data, leftValue: v } }
          }
          return retVal
        }) })
      },
      changeGasRight: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'gas') {
            retVal = { ...o, data: { ...o.data, rightValue: v } }
          }
          return retVal
        }) })
      },
      changeCleaningLeft: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'cleaning') {
            retVal = { ...o, data: { ...o.data, leftValue: v } }
          }
          return retVal
        }) })
      },
      changeCleaningRight: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'cleaning') {
            retVal = { ...o, data: { ...o.data, rightValue: v } }
          }
          return retVal
        }) })
      },
      changeMoldLeft: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'mold') {
            retVal = { ...o, data: { ...o.data, leftValue: v } }
          }
          return retVal
        }) })
      },
      changeMoldRight: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'mold') {
            retVal = { ...o, data: { ...o.data, rightValue: v } }
          }
          return retVal
        }) })
      },
      changeSkallSikringLeft: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'skallsikring') {
            retVal = { ...o, data: { ...o.data, leftValue: v } }
          }
          return retVal
        }) })
      },
      changeSkallSikringRight: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'skallsikring') {
            retVal = { ...o, data: { ...o.data, rightValue: v } }
          }
          return retVal
        }) })
      },
      changeTyveriSikringLeft: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'tyverisikring') {
            retVal = { ...o, data: { ...o.data, leftValue: v } }
          }
          return retVal
        }) })
      },
      changeTyveriSikringRight: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'tyverisikring') {
            retVal = { ...o, data: { ...o.data, rightValue: v } }
          }
          return retVal
        }) })
      },
      changeBrannSikringLeft: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'brannsikring') {
            retVal = { ...o, data: { ...o.data, leftValue: v } }
          }
          return retVal
        }) })
      },
      changeBrannSikringRight: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'brannsikring') {
            retVal = { ...o, data: { ...o.data, rightValue: v } }
          }
          return retVal
        }) })
      },
      changeVannskadeRisikoLeft: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'vannskaderisiko') {
            retVal = { ...o, data: { ...o.data, leftValue: v } }
          }
          return retVal
        }) })
      },
      changeVannskadeRisikoRight: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'vannskaderisiko') {
            retVal = { ...o, data: { ...o.data, rightValue: v } }
          }
          return retVal
        }) })
      },
      changeAlchoholStatus: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'alcohol') {
            retVal = { ...o, data: { ...o.data, statusValue: v } }
          }
          return retVal
        }) })
      },
      changeAlchoholVolume: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'alcohol') {
            retVal = { ...o, data: { ...o.data, volumeValue: v } }
          }
          return retVal
        }) })
      },
      changeAlchoholComment: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'alcohol') {
            retVal = { ...o, data: { ...o.data, commentValue: v } }
          }
          return retVal
        }) })
      },
      addPest: () => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          const p = { lifeCycle: '', count: 0 }
          if (o.type === 'pest') {
            retVal = { ...o, data: { ...o.data, observations: [...o.data.observations, p] } }
          }
          return retVal
        }) })
      },
      changeLifeCycle: (i, v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'pest') {
            const po = o.data.observations
            po[i] = { ...po[i], lifeCycle: v }
            retVal = { ...o, data: { ...o.data, observations: po } }
          }
          return retVal
        }) })
      },
      changeCount: (i, v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'pest') {
            const po = o.data.observations
            po[i] = { ...po[i], count: v }
            retVal = { ...o, data: { ...o.data, observations: po } }
          }
          return retVal
        }) })
      },
      changePestIdentification: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'pest') {
            retVal = { ...o, data: { ...o.data, identificationValue: v } }
          }
          return retVal
        }) })
      },
      changePestComment: (v) => {
        this.setState({ ...this.state, observations: this.state.observations.map((o) => {
          let retVal = o
          if (o.type === 'pest') {
            retVal = { ...o, data: { ...o.data, commentsValue: v } }
          }
          return retVal
        }) })
      }
    }

    this.observationTypes = {
      lux: defineCommentType('lux', 'Lysforhold',
      translate('musit.storageUnits.environmentRequirements.lightCondition.labelText'),
      translate('musit.storageUnits.environmentRequirements.lightCondition.tooltip'),
      translate('musit.storageUnits.environmentRequirements.renhold.comment'),
      translate('musit.storageUnits.environmentRequirements.renhold.comment'),
           this.actions.changeLuxLeft, this.actions.changeLuxRight),
      gas: defineCommentType('gas', 'Gass', translate('musit.storageUnits.environmentRequirements.gas.labelText'),
      translate('musit.storageUnits.environmentRequirements.gas.tooltip'),
      translate('musit.storageUnits.environmentRequirements.renhold.comment'),
      translate('musit.storageUnits.environmentRequirements.renhold.comment'),
           this.actions.changeGasLeft, this.actions.changeGasRight),
      cleaning: defineCommentType('cleaning', 'Renhold',
                translate('musit.storageUnits.environmentRequirements.renhold.labelText'),
                translate('musit.storageUnits.environmentRequirements.renhold.tooltip'),
                translate('musit.storageUnits.environmentRequirements.renhold.comment'), 'Right tooltip',
                this.actions.changeCleaningLeft, this.actions.changeCleaningRight),
      mold: defineCommentType('mold', 'Mugg', translate('musit.storageUnits.environmentRequirements.mold.labelText'),
              translate('musit.storageUnits.environmentRequirements.mold.tooltip'),
              translate('musit.storageUnits.environmentRequirements.renhold.comment'),
              translate('musit.storageUnits.environmentRequirements.renhold.comment'),
              this.actions.changeMoldLeft, this.actions.changeMoldRight),
      skallsikring: defineCommentType('skallsikring',
            'Skallsikring', translate('musit.storageUnits.environmentRequirements.skallsikring.labelText'),
            translate('musit.storageUnits.environmentRequirements.skallsikring.tooltip'),
            translate('musit.storageUnits.environmentRequirements.skallsikring.comment'),
            translate('musit.storageUnits.environmentRequirements.skallsikring.comment'),
            this.actions.changeSkallSikringLeft, this.actions.changeSkallSikringRight),
      tyverisikring: defineCommentType('tyverisikring',
            'Tyverisikring', translate('musit.storageUnits.environmentRequirements.tyverisikring.labelText'),
            translate('musit.storageUnits.environmentRequirements.tyverisikring.tooltip'),
            translate('musit.storageUnits.environmentRequirements.renhold.comment'),
            translate('musit.storageUnits.environmentRequirements.renhold.comment'),
            this.actions.changeTyveriSikringLeft, this.actions.changeTyveriSikringRight),
      brannsikring: defineCommentType('brannsikring',
            'Brannsikring', translate('musit.storageUnits.environmentRequirements.brannsikring.labelText'),
            translate('musit.storageUnits.environmentRequirements.brannsikring.tooltip'),
            translate('musit.storageUnits.environmentRequirements.brannsikring.comment'),
            translate('musit.storageUnits.environmentRequirements.brannsikring.comment'),
             this.actions.changeBrannSikringLeft, this.actions.changeBrannSikringRight),
      vannskaderisiko: defineCommentType('vannskaderisiko',
            'Vannskaderisiko', translate('musit.storageUnits.environmentRequirements.vannskaderisiko.labelText'),
            translate('musit.storageUnits.environmentRequirements.vannskaderisiko.tooltip'),
            translate('musit.storageUnits.environmentRequirements.vannskaderisiko.comment'),
            translate('musit.storageUnits.environmentRequirements.vannskaderisiko.comment'),
             this.actions.changeVannskadeRisikoLeft, this.actions.changeVannskadeRisikoRight),
      temperature: defineFromToType('temperature', 'Temperatur',
            translate('musit.storageUnits.environmentRequirements.temperature.labelText'),
            translate('musit.storageUnits.environmentRequirements.temperature.tooltip'),
            translate('musit.storageUnits.environmentRequirements.temperatureTolerance.labelText'),
            translate('musit.storageUnits.environmentRequirements.temperatureTolerance.labelText'),
            translate('musit.storageUnits.environmentRequirements.temperature.comment'),
            translate('musit.storageUnits.environmentRequirements.temperature.comment'),
            this.actions.changeTempFrom, this.actions.changeTempTo, this.actions.changeTempComment, '', ''),
      rh: defineFromToType('rh', 'Relativ luftfuktighet',
            translate('musit.storageUnits.environmentRequirements.relativeHumidity.labelText'),
            translate('musit.storageUnits.environmentRequirements.relativeHumidity.tooltip'),
            translate('musit.storageUnits.environmentRequirements.relativeHumidityTolerance.labelText'),
            translate('musit.storageUnits.environmentRequirements.relativeHumidityTolerance.labelText'),
            translate('musit.storageUnits.environmentRequirements.relativeHumidity.comment'),
            translate('musit.storageUnits.environmentRequirements.relativeHumidity.comment'),
            this.actions.changeRHFrom, this.actions.changeRHTo, this.actions.changeRHComment, '', ''),
      hypoxicAir: defineFromToType('hypoxicAir', 'Inert luft',
            translate('musit.storageUnits.environmentRequirements.inertAir.labelText'),
            translate('musit.storageUnits.environmentRequirements.inertAir.tooltip'),
            translate('musit.storageUnits.environmentRequirements.inertAirTolerance.labelText'),
            translate('musit.storageUnits.environmentRequirements.inertAirTolerance.labelText'),
            translate('musit.storageUnits.environmentRequirements.inertAir.comment'),
            translate('musit.storageUnits.environmentRequirements.inertAir.comment'),
            this.actions.changeHypoxicAirFrom, this.actions.changeHypoxicAirTo,
            this.actions.changeHypoxicAirComment, 'Fra %', 'Til %'),
      alcohol: defineStatusType('alcohol',
        translate('musit.storageUnits.environmentRequirements.alcohol.labelText'),
        translate('musit.storageUnits.environmentRequirements.alcohol.statusLabel'),
        'statusTooltip',
        [translate('musit.storageUnits.environmentRequirements.alcohol.statusItems.dryed'),
        translate('musit.storageUnits.environmentRequirements.alcohol.statusItems.allmostDryed'),
        translate('musit.storageUnits.environmentRequirements.alcohol.statusItems.someDryed'),
        translate('musit.storageUnits.environmentRequirements.alcohol.statusItems.minorDryed'),
        translate('musit.storageUnits.environmentRequirements.alcohol.statusItems.satisfactory')],
        translate('musit.storageUnits.environmentRequirements.alcohol.volume'),
        translate('musit.storageUnits.environmentRequirements.alcohol.tooltip'),
        translate('musit.storageUnits.environmentRequirements.alcohol.comment'),
        translate('musit.storageUnits.environmentRequirements.alcohol.comment'),
        this.actions.changeAlchoholStatus,
        this.actions.changeAlchoholVolume, this.actions.changeAlchoholComment),
      pest: definePestType('pest', 'Skadedyr', this.actions.addPest, this.actions.changeLifeCycle, this.actions.changeCount,
            this.actions.changePestIdentification, this.actions.changePestComment)
    }

    this.state = {
      observations: [
        /* {
          type: 'pest',
          data: {
            observations: [],
            identificationValue: '',
            commentsValue: ''
          }
        },
        {
          type: 'hypoxicAir',
          data: {
            fromValue: '',
            toValue: '',
            commentValue: ''
          }
        },
        {
          type: 'cleaning',
          data: {
            leftValue: '',
            rightValue: ''
          }
        },
        {
          type: 'alcohol',
          data: {
            statusValue: '',
            volumeValue: '',
            commentValue: ''      console.log(this.observationTypes[observation.type])
          }
        }*/
      ]
    }
    this.addNewObservation = this.addNewObservation.bind(this)
    this.onChangeDoneBy = this.onChangeDoneBy.bind(this)
    this.onChangeDate = this.onChangeDate.bind(this)
    this.selectType = this.selectType.bind(this)
  }

  onChangeDoneBy(event, { newValue }) {
    this.updateDoneBy(newValue)
  }

  onChangeDate(v) {
    this.setState({ ...this.state, date: v })
  }

  getDoneBySuggestionValue(suggestion) {
    return `${suggestion.fn}`
  }


  addNewObservation() {
    this.setState({ ...this.state, observations: [...this.state.observations, { type: '' }] })
  }

  selectType(index, observationType) {
    this.setState({
      ...this.state,
      observations: this.state.observations.map((obs, i) => {
        let retVal = obs
        if (index === i) {
          const typeValue = observationType[0]
          const obsType = observationType[1]
          retVal = { ...obs, type: typeValue, data: this.observationTypeDefinitions[obsType.component.viewType].defaultValues }
        }
        return retVal
      })
    })
  }

  updateDoneBy(newValue) {
    this.setState({ ...this.state, doneby: newValue })
  }

  renderDoneBySuggestion(suggestion) {
    const suggestionText = `${suggestion.fn}`
    return (
      <span className={'suggestion-content'}>{suggestionText}</span>
    )
  }

  render() {
    const { translate, onSaveObservation } = this.props
    const {
      addNewObservation,
      observationTypes,
      getDoneBySuggestionValue,
      renderDoneBySuggestion,
      onDoneByUpdateRequested,
      selectType
    } = this
    const { observations } = this.state

    const doneByProps = {
      id: 'doneByField',
      placeholder: 'Done by',
      value: 'value to be changed',
      type: 'search',
      onChange: this.onChangeDoneBy
    }

    const renderActiveTypes = (items) => {
      const block = items.map((observation, index) => {
        // TODO: Check for new type, how to handle STEIN: DONE (?)
        let subBlock = ''
        if (observation.type && observation.type.length > 0) {
          const observationType = this.observationTypes[observation.type]
          const observationTypeDef = this.observationTypeDefinitions[observationType.component.viewType]

          // TODO: Draw type field. And action to change type, with state reset and default variables. Stein:DONE
          subBlock = observationTypeDef.render(index, {
            ...observationType.component.props,
            ...observationTypeDef.props,
            ...observation.data
          })
        }
        const menuItems = Object.entries(observationTypes).map((obsType, row) => (
          <MenuItem
            key={row}
            disabled={this.state.observations.findIndex((e) => (e.type === obsType[0])) >= 0}
            eventKey={obsType}
          >
            {obsType[1].label}
          </MenuItem>
        ))
        return (
          <Row key={index}>
            <h1 />
            <Col sm={10} smOffset={1}>
              <SplitButton
                id={`new_${index}`}
                title={observation.type ? observationTypes[observation.type].label : null || 'Vennligst velg...'}
                pullRight
                onSelect={(observationType) => selectType(index, observationType)}
              >
                {menuItems}
              </SplitButton>
              <h1 />
              {subBlock}
            </Col>
          </Row>
        )
      })
      return block
    }

    return (
      <div>
        <main>
          <Panel>
            <Grid>
              <Row>
                <Col sm={10} smOffset={1}>
                  <FormGroup>
                    <Col xs={12} sm={6}>
                      <ControlLabel>{translate('musit.texts.date')}</ControlLabel>
                      <DatePicker
                        value={this.state.date}
                        onChange={this.onChangeDate}
                      />
                    </Col>
                    <Col xs={12} sm={6}>
                      <ControlLabel>{translate('musit.texts.doneBy')}</ControlLabel>
                      <Autosuggest
                        suggestions={[{ fn: 'test1' }, { fn: 'test2' }]}
                        onSuggestionsUpdateRequested={onDoneByUpdateRequested}
                        getSuggestionValue={getDoneBySuggestionValue}
                        renderSuggestion={renderDoneBySuggestion}
                        inputProps={doneByProps}
                        shouldRenderSuggestions={(v) => v !== 'undefined'}
                      />
                    </Col>
                  </FormGroup>
                </Col>
              </Row>
              <hr />
              {renderActiveTypes(observations)}
              <Row>
                <h1 />
                <Col sm={5} smOffset={1}>
                  <Button onClick={() => addNewObservation()}>
                    <FontAwesome name="plus-circle" /> {translate('musit.observation.newButtonLabel')}
                  </Button>
                </Col>
              </Row>
              <Row>
                <Col sm={10} smOffset={1} className="text-center">
                  <Button onClick={() => onSaveObservation({ data: this.state })}>
                    {translate('musit.texts.save')}
                  </Button>
                  <Button onClick={() => addNewObservation()}>
                    {translate('musit.texts.cancel')}
                  </Button>
                </Col>
              </Row>
            </Grid>
          </Panel>
        </main>
      </div>
    )
  }
}