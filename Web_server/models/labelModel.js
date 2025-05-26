let idCounter = 0
const labels = []

const getAllLabels = () => labels

const getLabel = (id) => labels.find(a => a.id === id)

const createLabel = (name) => {
     const newLabel = { id: ++idCounter, name} 
     labels.push(newLabel) 
     return newLabel }

const updateLabel = (id, name) => {
     const label = getLabel(id) 
     if (!label) return null
     if (name) label.name = name
     if (label.name === '') label.name = 'No Name'
     return label }

const deleteLabel = (id) => {
     const index = labels.findIndex(a => a.id === id)
     if (index === -1) return null
     return labels.splice(index, 1)[0] 
    }

module.exports = {
    getAllLabels,
    getLabel, 
    createLabel, 
    updateLabel, 
    deleteLabel 
}